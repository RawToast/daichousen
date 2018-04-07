import { Injectable } from '@angular/core';
import { Headers, Http } from '@angular/http';

import 'rxjs/add/operator/toPromise';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/observable/fromPromise';
import { GameResponse } from './gameresponse';
import { Subject } from 'rxjs/Subject';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';
import * as Config from '../config';

@Injectable()
export class GameService {

    constructor(private http: Http) { }
    private rootApiUrl = Config.chousenUri();  // URL to web api

    private headers = new Headers({'Content-Type': 'application/json'});
    private fetching: boolean;

    private gameResponse = new BehaviorSubject<GameResponse>(undefined);

    private myHeaders = new Headers({'Content-Type' : 'application/json'});

    private myInit = { method: 'POST',
               headers: this.myHeaders,
               mode: 'cors',
               cache: 'default' };

    create(name: string, choice: number): Promise<GameResponse> {
        const url = `${this.rootApiUrl}/game/${name}/start/${choice}`;
        return this.http
          .post(url, JSON.stringify({}))
          .toPromise()
          .then(response => {
            const result = response.json() as GameResponse;
            // tslint:disable-next-line:no-trailing-whitespace
            this.gameResponse.next(result);            
            return result;
        } )
          .catch(this.handleError);
    }

    makeRequest(uri: string, body: any): Promise<GameResponse> {
        const url = `${this.rootApiUrl}/${uri}`;
        return this.http
          .post(url, JSON.stringify(body), this.myInit)
          .toPromise()
          .then(response => {
            const result = response.json() as GameResponse;
            this.gameResponse.next(result);
            return result;
        } )
          .catch(this.handleError);
    }

    awaitData(id: string): Observable<GameResponse> {
        // console.log('Awaiting ' + id);

        if ((typeof this.gameResponse.getValue() === 'undefined') && !this.fetching) {
            this.refresh(id);
        }
        return this.getData();
    }

    refresh(id: string) {
        // console.log('Refreshing ' + id);
        const url = `${this.rootApiUrl}/game/${id}`;

        this.http.get(url).toPromise().then(data => {
            this.fetching = false;
            const result = data.json() as GameResponse;
            this.gameResponse.next(result);
        }).catch(this.handleError);

        return this.gameResponse;
    }

    getData(): Observable<GameResponse> {
        return this.gameResponse.asObservable();
    }

    private handleError(error: any): Promise<any> {
      console.error('An error occurred', error); // for demo purposes only
      this.fetching = false;
      this.gameResponse.error(error);
      return Promise.reject(error.message || error);
    }
}
