import { Component, OnInit, AfterViewInit } from '@angular/core';
import { GameService } from './game.service';
import { Defaults } from './defaults';
import { Action } from './gameresponse';
import { ActivatedRoute, ParamMap } from '@angular/router';
import { Location } from '@angular/common';

@Component({
    selector: 'chousen-input',
    template: `
    <div class="btn-group" role="group" *ngFor="let a of actions">
        <button *ngIf="a.request.length === 1" (click)="actionReq(a.uri, a.request[0])"
             type="button"
              data-toggle="tooltip" data-placement="top" title="{{ a.description }}"
              class="btn btn-secondary"> {{ a.name }} </button>

        <!-- Multi target -->
        <button *ngIf="a.request.length > 1" title="{{ a.description }}" type="button"
            class="btn btn-default dropdown-toggle" data-toggle="dropdown"
            data-placement="top" title="{{ a.description }}"
            aria-haspopup="true" aria-expanded="false" id="{{a.name}}">
                {{ a.name }}
                <span class="caret"></span>
        </button>
        <div *ngIf="a.request.length > 1" class="dropdown-menu" style="background-color: #1F1F1F;"
        attr.aria-labelledby="{{a.name}}" >
            <a class="dropdown-item c-dropdown" style="white-space: normal; background-color: #202020;"
            *ngFor="let t of a.request" (click)="actionReq(a.uri, t)">
                 {{ t.description }}
            </a>
        </div>
    </div>
        `
})

export class InputComponent implements OnInit {
    constructor(private gameService: GameService, private route: ActivatedRoute,
        private location: Location) { }

    actions: Action[] = new Defaults().EMPTY_GAME.actions;

        gameId = '';

        ngOnInit() {
            this.route.params.subscribe((params: ParamMap) => {
                const gid = params['id'];
                this.gameId = gid;
            });
            this.gameService.getData().subscribe(gs => {
                if (typeof gs !== 'undefined') {
                    this.actions = gs.actions;
                }
            });
        }

    actionReq(uri: string, req: Action) {
        this.gameService.makeRequest(uri, req);
    }

    block() {
        this.gameService.makeRequest(`game/${this.gameId}/block`, {});
    }
}
