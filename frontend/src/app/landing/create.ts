import { Component, OnInit } from '@angular/core';
import { Http } from '@angular/http';
import { GameService } from '../game/game.service';
import { Router } from '@angular/router';

@Component({
    selector: 'chousen-create',
    template: `<input id ="namebox" type="text" class="form-control" placeholder="Character Name" (keyup)="onKey($event)">
                <br>
                <fieldset>
                    <input type="radio" name="charc" value="2" (click)="classChoice(1)" checked> Adventurer
                </fieldset>`,
})

export class CreateGameComponent implements OnInit {
    constructor(private gameService: GameService, private router: Router) { }

    class = 1;
    ngOnInit() { }

    onKey(event: any) {
        if (event.which === 13) {
            event.preventDefault();

            this.gameService.create(event.target.value, this.class)
                .then(gr => {
                this.router.navigate([gr.uuid]);
            });
        }
    }

    classChoice(i: number) {
        this.class = i;
    }
}
