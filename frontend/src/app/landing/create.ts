import { Component, OnInit } from '@angular/core';
import { Http } from '@angular/http';
import { GameService } from '../game/game.service';
import { Router } from '@angular/router';

@Component({
    selector: 'chousen-create',
    template: `<input id ="namebox" type="text" class="form-control" placeholder="Character Name" (keyup)="onKey($event)">
                <br>
                <fieldset>
                    <input type="radio" name="charc" value="2" (click)="classChoice(1)" checked> Fighter
                    <input type="radio" name="charc" value="3" (click)="classChoice(2)"> Berserker
                    <input type="radio" name="charc" value="4" (click)="classChoice(3)"> Chieftain
                    <input type="radio" name="charc" value="4" (click)="classChoice(4)"> Rogue
                    <input type="radio" name="charc" value="5" (click)="classChoice(5)"> Trickster
                    <input type="radio" name="charc" value="6" (click)="classChoice(6)"> Mage
                    <input type="radio" name="charc" value="7" (click)="classChoice(7)"> Wizard
                    <input type="radio" name="charc" value="8" (click)="classChoice(8)"> Alchemist
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
