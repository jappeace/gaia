"""
This module can execute the program.
It deals with the command line options then cals the convert method which
starts the process.

Currently can:
+ Read AIML file structure and print a yaml file strcuture from it.
+ Generate drools based on insert tags
"""


import argparse

from aiml import convert

def main():
    """Run the program"""

    welcome = """
    This program can convert AIML, and S-AIML to YAML

    Note that:
    + "that" tags aren't converted
    + Drools probably need to be modified to work
    + xml needs to be informed about the coppied drools
    + In the xml document, no backward SRAI chain resolution, forward works
    + Star tags aren't converted
    + star's aren't filled in insert tag bodies.
    + Insert bodies are ignored, but this should be easily implementable
    + Sets are ignored
    """

    print(welcome)

    parser = argparse.ArgumentParser(description='Parse AIML into YAML, \n'+
        'just a proof of concept, doesn\'t support more exotic features of AIML')

    # TODO not default
    parser.add_argument('--input-resource-dir', dest='resource_dir', type=str,
        help='the resources directory',
        default="/home/jappie/Projects/thesis/communicate2/communicate/communicate_anamnesi/src/main/resources"
    )

    # TODO not default
    parser.add_argument('--scenario', dest='scenario', type=str,
        default="large",
        help='the name of the scenario, will be used as bots/{this value}'
    )

    parser.add_argument('--bots-dir', dest='bots_dir', type=str,
        help='the bots directory in resources where AIML bots can be found',
        default="bots"
    )

    parser.add_argument('--output-resource-dir', dest='output_dir', type=str,
        help='What bot dir to write into? Default is the same',
        default="/home/jappie/Projects/thesis/salve/salve/salve_anamnesi/src/main/resources"
    )

    parser.add_argument('--botname', dest='botname', type=str,
        default="patient",
        help='The name of the bot to be used, ie the role it '+
        'occupies, for example: patient'
    )

    parser.add_argument('--username', dest='user', type=str,
        default="doctor",
        help='This is the name of the user, ie the role the user has'+
        ', for example: doctor'
    )

    parser.add_argument('--drool-package-name', dest='droolpackage', type=str,
        default="sp.generated",
        help='package name of drools, for example sp.anamnesi'
    )
    args = parser.parse_args()
    botname = args.botname

    def indir(parrent, child):
        """put it into dir"""
        return "%s/%s" % (parrent, child)
    def in_resource_dir(child):
        """puts it in the resource dir"""
        return indir(args.resource_dir, child)
    botdir = in_resource_dir(args.bots_dir)

    aimldir = "%s/aiml/" % args.scenario
    inputdir = "%s/%s" % (botdir, aimldir)

    outresourcedir = args.output_dir if args.output_dir is not None else args.resource_dir
    outyaml = "%s/yml/" % indir(indir(outresourcedir, args.bots_dir), args.scenario)

    convert(inputdir, outyaml, botname, args.user, outresourcedir, args.droolpackage)

if __name__ == "__main__":
    main()
