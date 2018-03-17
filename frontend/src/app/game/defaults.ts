
import { GameResponse } from './gameresponse';

export class Defaults {

    EMPTY_GAME: GameResponse = {
        'uuid': '99999999-9999-9999-9999-999999999999',
        'player': {
            'name': 'UNKNOWN PLAYER',
            'className': 'UNKNOWN',
            'stats': {
                'maxHp': 70,
                'currentHp': 65,
                'strength': 7,
                'dexterity': 6,
                'intellect': 6,
                'vitality': 8,
                'speed': 8
            },
            'experience': {
                'current': 0,
                'next': 3,
                'level': 1
            },
            'equipment': {
                'weapon': {
                    'cardId': '20ffb469-bd25-4b0e-955e-0381ad8c9c2e',
                    'name': 'Short Sword',
                    'dmg': 6,
                    'requirements': {},
                    'effects': []
                },
                'armour': null
            },
            'gold' : 10,
            'position': 120,
            'status': [
                {
                    'effect': 'Regen',
                    'description': 'Regenerate health',
                    'turns': 5,
                    'amount': 5
                }
            ]
        },
        'cards': {
            'hand': [
                {
                    'name': 'Quick Attack',
                    'description': 'Attack with reduced movement penalty',
                    'id' : '0',
                    'charges': '(4/4)',
                    'playable': true,
                    'action': {
                        'name': 'Quick Attack',
                        'description': 'Attack with reduced movement penalty',
                        'uri': 'game/3c086ea5-5eda-4f31-bd09-21603bfed816/single/b1cb2f89-cc7f-4fa3-86be-2f100c780920',
                        'request': []
                    }
                },
                {
                    'name': 'Essence of Dexterity ',
                    'description': 'Immediately increases Dexterity, only 1 essence may be played per turn',
                    'playable': true,
                    'id' : '1',
                    'action': {
                        'name': 'Essence of Dexterity ',
                        'description': 'Immediately increases Dexterity, only 1 essence may be played per turn',
                        'uri': 'game/3c086ea5-5eda-4f31-bd09-21603bfed816/self/b9595c28-8435-4d94-8102-5370e93c51bb',
                        'request': [
                            {
                                'description': 'Essence of Dexterity ',
                                'action': 'EssenceOfDexterity'
                            }
                        ]
                    }
                },
            ],
            'equippedCards': {
                'skills': [
                    {
                        'name': 'Barrier',
                        'description': 'Creates a magic barrier to protect the user',
                        'id': 'd6857897-ddbe-4134-99dc-095c6f21d3e5',
                        'charges': '(3/5)',
                        'playable': true,
                        'action': {
                            'name': 'Barrier',
                            'description': 'Creates a magic barrier to protect the user',
                            'uri': 'game/ff56d7d5-cf80-4bec-8867-48974907c688/self/d6857897-ddbe-4134-99dc-095c6f21d3e5',
                            'request': [
                                {
                                    'description': 'Barrier',
                                    'action': 'Barrier'
                                }
                            ]
                        }
                    }
                ]
                // 'weapon': null,
                // 'armour': null,
                // 'jewelery': null
            }
        },
        'currentEncounter': {
            'enemies': []
        },
        'actions': [
            {
                'name': 'Attack Sloth',
                'description': 'Use a basic attack on Sloth',
                'uri': 'game/e02c84a3-8d95-4df2-b8d0-56f065ebc0ba/attack',
                'request': [
                    {
                        'targetId': '7daa7e57-ef61-4cc9-95d5-13fd773d75c9'
                    }
                ]
            },
            {
                'name': 'Attack Slime',
                'description': 'Use a basic attack on Slime',
                'uri': 'game/e02c84a3-8d95-4df2-b8d0-56f065ebc0ba/attack',
                'request': [
                    {
                        'targetId': '1254c142-ec4a-475e-a9f8-1c63f7b28b5b'
                    }
                ]
            },
            {
                'name': 'Block',
                'description': 'Block for one turn, greatly increasing defense',
                'uri': 'game/e02c84a3-8d95-4df2-b8d0-56f065ebc0ba/block',
                'request': [
                    {}
                ]
            }
        ],
        'messages': [
            'the game has not loaded'
        ]
    };

}
