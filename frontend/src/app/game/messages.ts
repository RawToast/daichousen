import { Component, OnInit } from '@angular/core';
import { GameService } from './game.service';
import { Defaults } from './defaults';

@Component({
    selector: 'chousen-messages',
    template: `
    <div id="gameMessagesRow" class="row" style="padding: 8px 0px">
        <div id="gameMessagesOffSet" class="col-sm-10">
            <textarea id="gameMessagesText" class="form-control noresize c-messages" rows="12" data-role="none">
{{getMessages()}}
            </textarea>
        </div>
    </div>`
})

export class MessagesComponent implements OnInit {
    constructor(private gameService: GameService) { }

    messages: string[] = new Defaults().EMPTY_GAME.messages;

    ngOnInit() {
        this.gameService.getData().subscribe(gs => {
            if (typeof gs !== 'undefined') {
                this.messages = gs.messages;
            }
        });
    }

    getMessages() {
        if (this.messages.length > 1) {
            return this.messages.reduce((p, c) => p + '\r\n' + c);
        } else {
            return this.messages[0];
        }
    }
}
