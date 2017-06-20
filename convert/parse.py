"""
Parses AIML file into a Result structure
"""

import xml.etree.ElementTree as ET
from collections import namedtuple

# data structure
Symbol = namedtuple('Symbol', ['scene', 'name', 'literals', 'patterns', 'regexes', 'inserts'])
Insert = namedtuple('Insert', ['type', 'values'])
Connection = namedtuple('Connection', ['frm', 'to', 'before'])
Result = namedtuple('Result', ['scene_name', 'connections', 'symbols'])

class Tags:
    """
    The known xml tags of AIML, just to group this info together,
    and give aditional documentation
    """
    # pattern matching tag
    pattern = "pattern"
    # reference tag (to another category)
    srai = "srai"
    # S-AIML extension, groups categories together, works together with drools
    scene = "scene"
    # A response
    template = "template"
    # groups pieces of knowledge together, usually a pattern and a template
    category = "category"
    # refers back to some previous thing
    that = "that"
    # insert a type into drools
    insert = "insert"

_default_scene_name = "default"

def text(tag):
    """extract words from the tag"""
    result = ""
    if tag.text is not None:
        result = tag.text.strip()
    for child in tag:
        if child.tail is None:
            continue
        result += child.tail.strip()
    return result

def name(text):
    """
    prepare names to be filenames,
    some filesystems may not appreciate special characters in names
    (ntfs, although only the UI)
    there are a bunch of little no-no's that are fixed by this function.
    It has an added advantage that the key->symbol mapping becomes better
    """
    space = ' '

    # only appreciate alfanumeric characters in names (and spaces)
    alfanumunder = ''.join(e for e in text if e.isalnum() or e == space)

    # case sentivity is a bad idea
    lowercase = alfanumunder.lower()

    # yes, trailing whitespaces, that's what we want
    stripped = lowercase.strip()
    betterspace = '_'

    # replace spaces with betterspaces
    better_spaced = stripped.replace(space, betterspace)

    # if there were special characters we can now have__this, fix that.
    return better_spaced.replace(betterspace+betterspace, betterspace)

def addLiteral(result, key, text, pattern):
    """add literal, used with insert"""
    result.symbols[key].literals.append(text)

def addPattern(result, key, text, pattern):
    """add patern to result, used with insert"""
    result.symbols[key].patterns.append(pattern)

def isGoodKey(key):
    """Check if key isn't pointless"""
    return key != ''

def createInsertFrom(tag):
    """extact inforamtion from tag"""
    _type = "%s.%s" % (tag.get("packageName"), tag.get("typeName"))
    return Insert(_type, text(tag))

class Parser:
    """A parser from AIML to a Result (known structure)"""
    def __init__(self):
        # used references
        self.references = {}
    def AIMLtoResult(self, filename):
        """filename = string"""
        tree = ET.parse(filename)
        root = tree.getroot()
        scene_name = _default_scene_name
        scene = root.find(Tags.scene)
        if scene is None:
            scene = root
        else:
            scene_name = scene.attrib["name"]

        scene_name = name(scene_name)

        result = Result(scene_name, [], {})
        for child in scene:

            if child.tag != Tags.category:
                print("what is %s? Unkown format, skipping" % child.tag)
                continue

            # needed anyway
            patternText = text(child.find(Tags.pattern))

            templateTag = child.find(Tags.template)

            # check for the reference
            sraiTag = templateTag.find(Tags.srai)
            if sraiTag is not None:
                # we have a srai tag, this means no connection but adding a
                # pattern to an existing
                sraitext = text(sraiTag)
                refferingTo = name(sraitext)
                self._insert(scene_name, result, refferingTo, sraitext, patternText)
                continue

            templateText = text(templateTag)
            templateKey = name(templateText)


            self._insert(scene_name, result, templateKey, templateText, ifExists=addLiteral)

            # extract the insert tags from template and insert them in pattern
            # we need to bind the inserts to the pattern, because they will be
            # injected (by drools) when the pattern 'symbol' is uttered
            tags = [createInsertFrom(tag) for tag in templateTag.findall(Tags.insert)]
            patternKey = name(patternText)
            self._insert(scene_name, result, patternKey, patternText, patternText, inserts=tags)

            if not isGoodKey(patternKey) or not isGoodKey(templateKey):
                print("ignoring connection because defunct: %s -> %s" % (patternKey, templateKey))
                continue

            thatTag = child.find(Tags.that)
            before = None if thatTag is None else name(text(thatTag))

            result.connections.append(
                Connection(frm=patternKey, to=templateKey, before=before)
            )
        return result

    def _insert(self, scene, result, key, text, pattern=None, inserts=[], ifExists=addPattern):
        """Insert a pattern into the symbol or create a new one"""
        if not isGoodKey(key):
            return

        # To deal with srai reference to srai reference
        # This only works downards though, making it upwards would be more
        # involved 
        key = key if key not in self.references else self.references[key]

        if key in result.symbols:
            reference = name(pattern if pattern is not None else text)
            self.references[reference] = key
            ifExists(result, key, text, pattern)
        else:
            patterns = [] if pattern is None else [pattern]
            result.symbols[key] = Symbol(
                scene, key, [text], patterns, [], inserts
            )
