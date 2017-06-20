"""
Write Result structre from parsing into into yaml file structre
"""

import os

import yaml

import parse
from drools import writedrools

def symbol_as_content(symbol):
    """extracts the information for the yaml symbol file"""
    dic = symbol._asdict()
    # name gets deleted since its in the filename
    del dic["name"]
    del dic["inserts"]
    del dic["scene"]
    # delete ordering, creating a nice flat yaml dict
    # (rather than reffering python types)
    return dict(dic)

def connection_as_dict(connection, botname):
    """
    change the connection structure to the YAML one (which is much more complicated)
    In yaml we can do multiple froms to multiple to's for example.
    This is impossible in AIML, and this script can't detect that
    """
    dic = connection._asdict()

    dic["from"] = [dic["frm"]]
    del dic["frm"]

    dic["to"] = [{
        "symbol": dic["to"],
        "restricted_to": botname,
    }]

    beforekey = "before"
    if dic[beforekey] is None:
        del dic[beforekey]
    else:
        dic[beforekey] = {
            "who": botname,
            "said": dic[beforekey]
        }

    return dict(dic)

def writeScene(result, writer, scenedir, botname):
    """
    write a yaml 'scene', which is a directory. 
    This corosponds to a single AIML file.
    the yaml scheme uses filenames to gaurantee uniquness.
    """
    if not os.path.exists(scenedir):
        os.makedirs(scenedir)

    connectionsFileName = '_connections.yml'
    connectionsFile = '%s/%s' % (scenedir, connectionsFileName)

    print("writing %s", connectionsFile)

    def connection_to_yamlblock(connection):
        result = '---\n'
        result += yaml.dump(
            connection_as_dict(connection, botname),
            default_flow_style=False
        )
        return result

    connectionYaml = ''.join(
        [connection_to_yamlblock(conn) for conn in result.connections]
    )
    writer.write(connectionsFile, connectionYaml)

    for symbolName in result.symbols:
        filename = "%s.yml" % symbolName
        symbolFile = "%s/%s" % (scenedir, filename)
        print("writing %s", symbolFile)
        writer.write(
            symbolFile,
            yaml.dump(symbol_as_content(result.symbols[symbolName]))
        )

class IndexTracker:
    """Keep track of all written files to write an index file from"""
    def __init__(self, rootpath):
        self.index = ""
        self.rootpath = rootpath + "/"
    def write(self, filename, string):
        with open(filename, 'w') as writefile:
            writefile.write(string)
        self.index += "%s\n" % filename.replace(self.rootpath, '')
    def __str__(self):
        return self.index

def convert(inputdir, outyaml, botname, user, out_resource_dir, drool_package_name):
    """convert AIML file to YAML structure in outyaml"""
    index = IndexTracker(outyaml)
    results = []
    for aimlFile in os.listdir(inputdir):
        if not aimlFile.endswith('.aiml'):
            continue
        print("parsing %s" % aimlFile)
        targetfile = '%s/%s' % (inputdir, aimlFile)
        parser = parse.Parser()
        result = parser.AIMLtoResult(targetfile)
        scenedir = "%s/%s" % (outyaml, result.scene_name)
        writeScene(result, index, scenedir, botname)
        results.append(result)

    writedrools(results, index, out_resource_dir, drool_package_name)

    nonsense = parse.Symbol(parse._default_scene_name, 'nonsense', ['That\'ts ludicrious!'], [], [], [])
    index.write("%s/%s.yml" % (outyaml, nonsense.name), yaml.dump(symbol_as_content(nonsense)))

    believes = {
        "goals" : [],
        "values" : {},
        "self" : botname,
        "actors" : [botname, user],
        "personality" : ["Ti", "Se", "Ni", "Fe", "Te", "Si", "Ne", "Fi"]
    }
    index.write("%s/believes.yml" % outyaml, yaml.dump(believes))

    with open("%s/index.txt" % outyaml, 'w') as indexfile:
        indexfile.write(str(index))
