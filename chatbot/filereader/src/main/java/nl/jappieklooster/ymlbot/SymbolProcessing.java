package nl.jappieklooster.ymlbot;

import com.google.common.collect.Sets;
import org.salve.drools.model.Symbol;
import nl.jappieklooster.ymlbot.model.RawSymbol;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.vfs2.*;
import org.javatuples.Pair;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Handles loading of symbols from the user scripts
 */
class SymbolProcessing{
	private static final Set<String> ignoreSet = Sets.newHashSet(YapFactory.believes, YapFactory.connections_name);

	static Pair<Map<String, Symbol>, Map<Symbol, Set<Pattern>>>
            processSymbols(Map<String, RawSymbol> symbol_table){
        final Map<String, Symbol> processed_symbol_table = new HashMap<>();
        final Map<Symbol , Set<Pattern>> string_symbol_map = new HashMap<>();

        for (Map.Entry<String, RawSymbol> entry : symbol_table.entrySet()){
            final RawSymbol raw = entry.getValue();
            final String key = entry.getKey();
			final String folder = FilenameUtils.getPathNoEndSeparator(key);
            final String name = folder.equals(YapFactory.yml_dir) ?
					FilenameUtils.getName(FilenameUtils.removeExtension(key)) : // its the root dir
					FilenameUtils.removeExtension(key);
            raw.name = name;
            raw.scene = folder.equals(YapFactory.yml_dir) ? "" : folder;
            System.out.println("processing " + folder + "->" + name + ", stored as: " + key);

            final Set<Pattern> patterns = new HashSet<>();
            patterns.addAll(raw.literals.stream().map(Pattern::compile).collect(Collectors.toSet()));
            patterns.addAll(raw.regexes.stream().map(Pattern::compile).collect(Collectors.toSet()));
            patterns.addAll(raw.patterns.stream().map(SymbolProcessing::preProcessPattern).collect(Collectors.toSet()));

            final Symbol symbol = raw.create();
            validate(symbol);
            processed_symbol_table.put(name, symbol);

            if (!string_symbol_map.containsKey(symbol)){
                string_symbol_map.put(symbol, new HashSet<>());
            }
            string_symbol_map.get(symbol).addAll(patterns);
        }
        return new Pair<>(processed_symbol_table, string_symbol_map);
    }

    /**
     * Throws (runtime) exceptions
     * @param symbol
     */
    static void validate(Symbol symbol){
    	// do validations
    }

    static final Pattern replacer = Pattern.compile("\\*");
    static final String replaceString = "(.*)";

    /**
     * fix simple * syntax towards more general rebexes
     * @param str
     * @return
     */
    static Pattern preProcessPattern(String str){
        return Pattern.compile(replacer.matcher(str).replaceAll(replaceString));
    }
    static Map<String, RawSymbol> parseFolderIntoSymbols(FileObject folder){
		final Map<String, RawSymbol> symbol_table = new HashMap<>();
		YapFactory.getChildrenRecursively(folder)
		.filter(x -> {
			System.out.println(x.getName().getBaseName());
			return !ignoreSet.contains(x.getName().getBaseName());
		})
		.forEach(file -> {
			List<RawSymbol> syms = YapFactory.readAsYML(file, RawSymbol.class);
			if(syms.size() > 1){
				throw new RuntimeException("Can't deal with multiple yml " +
						"objects in symbol file, please use different files " +
						"for symbols so that their names are gauranteed to " +
						"be unique, error in " + file.getName().getBaseName());
			}
			RawSymbol sym = syms.iterator().next();
			sym.name = file.getName().getBaseName();
			symbol_table.put(
					getFromRootName(file),
					sym
			);
		}); //
		return symbol_table;
    }
    public static String getFromRootName(FileObject currentFile){
		try {
			return currentFile.getParent().getParent().getName().getRelativeName(
				currentFile.getName()
			);
		} catch (FileSystemException e) {
			YapFactory.handleIOException(e);
		}
		return null;
	}
}
