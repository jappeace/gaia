"""
Use the jinjia template to write drools for the inserts
"""

from jinja2 import Environment, FileSystemLoader
import os

def writedrools(results, index, out_resource_dir, drool_package_name):
    # get all symbols with insert statements (its a flatmap)
    withInsert = [
        result.symbols[symbolname]
        # unpack result from all results
        for result in results
        # unpack symbolname from the unpacked result
        for symbolname in result.symbols
        # filter, only for relevant symbols
        if not result.symbols[symbolname].inserts == []
    ] 

    env = Environment(
        loader=FileSystemLoader('%s/templates/' % os.path.dirname(__file__))
    )
    template = env.get_template('inserts.drl')
    drools = template.render(symbols=withInsert, package=drool_package_name)

    drooldir = '%s/%s' % (out_resource_dir, drool_package_name.replace('.', '/'))

    if not os.path.exists(drooldir):
        os.makedirs(drooldir)
    
    filename = "%s/generated.drl" % drooldir

    with open(filename, 'w') as droolfile:
        droolfile.write(drools)
