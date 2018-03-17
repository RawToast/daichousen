import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, ParamMap } from '@angular/router';
import { Location } from '@angular/common';

import { GameResponse } from './gameresponse';
import { GameService } from './game.service';

import 'rxjs/add/operator/switchMap';
import { Defaults } from './defaults';

@Component({
    selector: 'chousen-game',
    template: `
    <chousen-player></chousen-player>

    <chousen-enemy></chousen-enemy>

    <chousen-messages></chousen-messages>

    <chousen-input></chousen-input>

    <chousen-skills></chousen-skills>

    <chousen-cards></chousen-cards>
    `,
    styleUrls: ['./game.component.css']
})

export class GameComponent implements OnInit {

    game: GameResponse = new Defaults().EMPTY_GAME;

    constructor(private gameService: GameService,
        private route: ActivatedRoute, private location: Location) { }

    ngOnInit() {
        this.route.params.subscribe((params: ParamMap) => {
            const gid = params['id'];
            this.gameService.awaitData(gid);
            this.gameService.getData().subscribe(gs => {
                if (typeof gs !== 'undefined') {
                    this.game = gs;
                }
            });
        });
    }
}
