package nl.jappieklooster.ymlbot;

import org.apache.commons.vfs2.FileObject;
import org.salve.drools.model.Connection;
import org.salve.drools.model.Symbol;
import org.salve.drools.model.db.SymbolDatabase;
import nl.jappieklooster.ymlbot.model.RawConnections;
import nl.jappieklooster.ymlbot.model.RawConnection;
import org.apache.commons.io.FilenameUtils;
import org.javatuples.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Handles loading of connections from the user scripts
 */
public class ConnectionProcessing{
    static Map<Symbol, Set<Connection>> processConnections(SymbolDatabase db, FileObject folder) {
		final Map<Symbol, Set<Connection>> conn_table = new HashMap<>();
		YapFactory.getChildrenRecursively(folder).map(x ->
			new Pair<>(x,
				x.getName().getBaseName()
			)
		).filter(x -> x.getValue1().equals(YapFactory.connections_name))
			.map(Pair::getValue0)
			.flatMap(file ->
				YapFactory.readAsYML(file, RawConnections.class).stream()
					.map( conn -> {
						final String fileName = SymbolProcessing.getFromRootName(file);
						final String fpath = FilenameUtils.getPathNoEndSeparator(fileName);
						final String scene = fpath.equals(YapFactory.yml_dir) ? "" : FilenameUtils.getPath(fileName);
						return new Pair<>(scene, conn);
					})
			)
			.forEach(tupple -> {
				final RawConnections rawcons = tupple.getValue1();
				final Set<Connection> connections = rawcons.to.stream()
					.map(rawcon -> {
						RawConnection conn = RawConnection.createDefaults();
						conn.before = rawcons.before;
						return rawcon.overwriteIfDefault(conn);
					})
					.map(rawcon -> rawcon.overwriteIfDefault(rawcons.to_defaults))
					.map(rawcon -> {
						if(rawcon.scene.equals(RawConnection.same_scene)){
							rawcon.scene = tupple.getValue0();
						}
						return rawcon;
					})
					.map(x -> {
						try {
							return x.create(db);
						} catch (RawConnection.TemplateAttributeNotFoundException e) {
							throw new RuntimeException(
								"While trying to make connections from " + rawcons.from +
									", " + e.getMessage(), e);
						}
					})
					.collect(Collectors.toSet());
				rawcons.from.stream()
					.map(x -> tupple.getValue0() + x)
					.flatMap(str -> db.get(str)
						.map(symbol -> Stream.of(new Pair<>(str, symbol)))
						.orElseGet(() ->{
							Logger.getLogger("construction").warning("Ignoring " + str + " because not found");
							return Stream.empty();
						}))
					.forEach(pair ->{
						Symbol from = pair.getValue1();
						if (conn_table.containsKey(from)) {
							conn_table.get(from).addAll(connections);
						} else {
							conn_table.put(from, connections);
						}
					});
			});
		return conn_table;
	}
}
