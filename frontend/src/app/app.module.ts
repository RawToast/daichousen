import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppRoutingModule } from './app.routes.module';

import { AppComponent } from './app.component';
import { CreateGameComponent } from './landing/create';
import { PatchNotesComponent } from './landing/notes';
import { LandingComponent } from './landing/landing';
import { GameComponent } from './game/game.component';
import { PlayerComponent } from './game/player';
import { GameService } from './game/game.service';

import { HttpModule, Http } from '@angular/http';
import { EquipmentComponent } from './game/equipment';
import { EnemyComponent } from './game/enemy';
import { MessagesComponent } from './game/messages';
import { InputComponent } from './game/input';
import { CardsComponent } from './game/cards';
import { GameResponse } from './game/gameresponse';
import { GuideComponent } from './landing/guide';
import { SkillsComponent } from './game/skills';

@NgModule({
  declarations: [
    AppComponent,
    CreateGameComponent,
    PatchNotesComponent,
    GuideComponent,
    LandingComponent,

    GameComponent,
    PlayerComponent,
    EquipmentComponent,
    EnemyComponent,
    MessagesComponent,
    CardsComponent,
    SkillsComponent,

    InputComponent
  ],
  imports: [
    AppRoutingModule,
    BrowserModule,
    HttpModule
  ],
  providers: [GameService],
  bootstrap: [AppComponent]
})
export class AppModule { }
