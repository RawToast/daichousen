import { Component, OnInit } from '@angular/core';
import { GameService } from './game.service';
import { Player, Enemy } from './gameresponse';
import { Defaults } from './defaults';

@Component({
    selector: 'chousen-enemy',
    template: `
    <div id="enemySheet" class="row enemySheet">
        <div class="col-sm-2" style="padding: 2px 0px" *ngFor="let e of enemies">
            <div class="col-sm-12">{{e.name}}</div>
            <div class="col-sm-12">HP {{e.stats.currentHp}} / {{e.stats.maxHp}}</div>
            <div class="col-sm-12">{{ statusMessage(e) }}</div>
        </div>
    </div>
    `
})

export class EnemyComponent implements OnInit {

    constructor(private gameService: GameService) {}

        enemies: Enemy[] = new Defaults().EMPTY_GAME.currentEncounter.enemies;

        ngOnInit() {
            this.gameService.getData().subscribe(gs => {
                if (typeof gs !== 'undefined') {
                    this.enemies = gs.currentEncounter.enemies.sort((a, b) => a.id.localeCompare(b.id));
                }
            });
        }

        statusMessage(e: Enemy) {
            const effects = e.status.map(s => s.effect);

            if (effects.length > 0) {
                return 'ST: ' + effects.filter((ef, i) => effects.indexOf(ef) === i)
                                        .reduce((l, r) => l + ', ' + r);
            } else {
                return '';
            }
        }
}
