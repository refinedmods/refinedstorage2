import os
import json
import shutil

shutil.rmtree('output/')


def create_file(path, contents):
    print('Generating ' + path)
    try:
        os.makedirs(os.path.dirname(path))
    except:
        pass
    with open(path, 'w') as file:
        file.write(contents)


def to_json(data):
    return json.dumps(data, indent=2)


def get_color_key(color, name):
    if color == 'light_blue':
        return name
    return color + '_' + name


def generate_north_cutout_block_model(name, particle, north, east, south, west, up, down, cutout, fullbright_cutout):
    parent = 'refinedstorage2:block/fullbright_north_cutout' if fullbright_cutout else 'refinedstorage2:block/north_cutout'

    create_file('output/models/block/' + name + '.json', to_json({
        'parent': parent,
        'textures': {
            'particle': particle,
            'north': north,
            'east': east,
            'south': south,
            'west': west,
            'up': up,
            'down': down,
            'cutout': cutout
        }
    }))


def generate_referencing_item_model(name, reference):
    create_file('output/models/item/' + name + '.json', to_json({
        'parent': reference
    }))


bi_direction_rotations = {
    'up_north': {
        'x': -90,
        'y': 180
    },
    'up_east': {
        'x': -90,
        'y': -90
    },
    'up_south': {
        'x': -90,
        'y': 0
    },
    'up_west': {
        'x': -90,
        'y': 90
    },
    'down_north': {
        'x': 90
    },
    'down_east': {
        'x': 90,
        'y': 90
    },
    'down_south': {
        'x': 90,
        'y': 180
    },
    'down_west': {
        'x': 90,
        'y': -90
    },
    'north': {},
    'east': {
        'y': 90
    },
    'south': {
        'y': 180
    },
    'west': {
        'y': 270
    }
}


def generate_blockstate_for_each_bi_direction(name, model_factory):
    result = {
        'variants': {}
    }

    for direction in bi_direction_rotations.keys():
        result['variants']['direction=' + direction] = {
            'model': model_factory(direction),
            'x': bi_direction_rotations[direction].get('x', 0),
            'y': bi_direction_rotations[direction].get('y', 0)
        }

    create_file('output/blockstates/' + name + '.json', to_json(result))


with open('colors.txt') as colors_file:
    colors = colors_file.read().splitlines()
    for color in colors:
        generate_north_cutout_block_model('grid/' + color, particle='refinedstorage2:block/grid/right', east='refinedstorage2:block/grid/right', south='refinedstorage2:block/grid/back', west='refinedstorage2:block/grid/left',
                                          up='refinedstorage2:block/grid/top', down='refinedstorage2:block/bottom', north='refinedstorage2:block/grid/front', cutout='refinedstorage2:block/grid/cutouts/' + color, fullbright_cutout=True)
        generate_referencing_item_model(
            get_color_key(color, 'grid'), 'refinedstorage2:block/grid/' + color)
        generate_blockstate_for_each_bi_direction(get_color_key(
            color, 'grid'), lambda direction: 'refinedstorage2:block/grid/' + color)

    generate_north_cutout_block_model('grid/disconnected', particle='refinedstorage2:block/grid/right', east='refinedstorage2:block/grid/right', south='refinedstorage2:block/grid/back', west='refinedstorage2:block/grid/left',
                                      up='refinedstorage2:block/grid/top', down='refinedstorage2:block/bottom', north='refinedstorage2:block/grid/front', cutout='refinedstorage2:block/grid/cutouts/disconnected', fullbright_cutout=False)
