package nl.jappieklooster.ymlbot;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import nl.jappieklooster.ymlbot.model.RawBelieves;
import org.salve.drools.model.*;
import org.salve.drools.model.db.ConnectionDatabase;
import org.salve.drools.model.db.PatternCollection;
import org.salve.drools.model.db.PatternDatabase;
import org.salve.drools.model.db.SymbolDatabase;
import org.salve.drools.model.values.PerlocutionaryValue;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.vfs2.FileSystemException;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.salve.personality.model.Believes;

/**
 * To create a YBot from YML this class can be used.
 */
public class YapFactory{
	static final String bots_dir = "bots";
	static final String yml_dir = "yml";
	static final String connections_name = "_connections.yml";

	static final String believes = "believes.yml";

	public static final String default_scene = "default";
    public Triplet<Believes, SymbolDatabase, PatternCollection> create(FileObject folder) {
    	// this is why other langauges have type inference
        final Pair<Map<String, Symbol>, Map<Symbol, Set<Pattern>>>
				symbols = SymbolProcessing.processSymbols(
						SymbolProcessing.parseFolderIntoSymbols(folder)
		);

        final SymbolDatabase symbolDatabase = new SymbolDatabase(symbols.getValue0());
        final ConnectionDatabase connDatabase = new ConnectionDatabase(
				ConnectionProcessing.processConnections(symbolDatabase, folder)
			);

		Believes believes = createBelieves(folder, connDatabase, symbolDatabase);
        checkForUnkowns(believes);

        return new Triplet<>(
        	believes,
			symbolDatabase,
			new PatternCollection(
				PatternProcessing.createSceneContained(symbols.getValue1())	,
				PatternProcessing.createSceneNextTo(symbols.getValue1(), connDatabase)
			)
		);
    }

	/**
	 * check if the connection database contains anything that isn't defined
	 * in the believe structure, such as actors and personal values.
	 * @throws RuntimeException if there are any unkowns so that user can fix them
	 */
	public static void checkForUnkowns(Believes believes){
		Set<Actor> actors = believes.getActors().collect(Collectors.toSet());
		actors.add(Actor.any); // hascode check for any
		believes.programmedConnections.values()
			.flatMap(Set::stream)
			.forEach(connection -> {
				if(!actors.contains(connection.restricted_to)){
					throwBelieveException(
						"could not find " + connection.restricted_to +
						" in the believed actor set " + actors
					);
				}
				for(PerlocutionaryValue value : connection.values){
					if(!believes.values.contains(value)){
						throwBelieveException(
							"Could not find value "+ value +
							"  in believe values: " + believes.values
						);
					}
				}
		});
	}
	public static void throwBelieveException(String message){
		throw new RuntimeException(message +
			" please define it in your believes.yml, or change it in its respective _connections file"
		);
	}

    public static Believes createBelieves(FileObject folder, ConnectionDatabase conss, SymbolDatabase symbols){
		try {
			FileObject believesFile = folder.resolveFile(believes);
			RawBelieves raw = readAsYML(believesFile, RawBelieves.class).get(0);
			return raw.create(conss, symbols);
		} catch (FileSystemException e) {
			handleIOException(e);
		}
		throw new RuntimeException("never come here ok?");
	}

	static String createRootPath(String path, String botname){

		final String dirSep = File.separator;
    	StringBuilder builder = new StringBuilder();
    	builder.append(path);
    	builder.append(dirSep);
    	builder.append(bots_dir);
		builder.append(dirSep);
		builder.append(botname);
		builder.append(dirSep);
		builder.append(yml_dir);
		builder.append(dirSep);
		return builder.toString();
	}

	/**
	 * Get children whilest dealing with the checked exception
	 * (by making it runtime exception)
	 *
	 * Also opens all folders and gets the files in those folders
	 * @param folder
	 * @return
	 */
	static Stream<FileObject> getChildrenRecursively(FileObject folder) {
		try {
			return Stream.of(folder.getChildren()).flatMap(file ->
				isFolder(file) ?
				getChildrenRecursively(file) :
				Stream.of(file)
			);
		} catch (FileSystemException e) {
			YapFactory.handleIOException(e);
		}
		// unreachable code because IOexception throws runtime exception
		return Stream.empty();
	}
	static boolean isFolder(FileObject file){
		try {
			return file.isFolder();
		} catch (FileSystemException e) {
			handleIOException(e);
		}
		// unreachable
		return false;
	}
	static void handleIOException(IOException e){
		throw new RuntimeException("file not found: " + e.getMessage(), e);
	}

	/**
	 * wrapper method for YML api,
	 * catches all exceptions and rethrows them as runtime exceptions.
	 * We want the program to crash if the user configures it wrong.
	 * Also always returns a list becasue the yml standard allows for
	 * multiple instances of objects in a file. (but this is defined rather
	 * implicetly by the read api)
	 *
	 * @param file
	 * @param clas
	 * @param <T>
	 * @return list cause user can put multiple YML objects in a single file
	 */
	static <T> List<T> readAsYML(FileObject file, Class<T> clas){
		final FileName fileName = file.getName();
		final String fileName_string = fileName.getBaseName();
		try {
			final YamlReader reader = new YamlReader(
					new InputStreamReader(file.getContent().getInputStream(), "UTF8")
			);
			List<T> result = new LinkedList<T>();
			while(true){
				T temp = reader.read(clas);
				if(temp == null){
					break;
				}
				result.add(temp);
			}
			return result;
		} catch (YamlException e) {
			// since the yml gets compiled into the jar, we assume its a dev error
			throw new RuntimeException(
				"yml invalid of: " + fileName_string + ", reason: " + e.getMessage(), e
			);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(
					"Encoding issues?" + fileName_string + ", reason: " + e.getMessage(), e
			);
		} catch (FileSystemException e) {
			throw new RuntimeException(
					"filesystem??" + fileName_string + ", reason: " + e.getMessage(), e
			);
		}
	}
}
