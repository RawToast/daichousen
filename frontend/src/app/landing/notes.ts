import { Component, OnInit } from '@angular/core';

@Component({
    selector: 'chousen-notes',
    templateUrl: 'notes.html'
})

export class PatchNotesComponent implements OnInit {
    constructor() { }
    showNotes = false;


    ngOnInit() { }

    toggleNotes() {
        this.showNotes = !this.showNotes;
    }
}
