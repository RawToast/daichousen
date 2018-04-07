import { Component, OnInit } from '@angular/core';

@Component({
    selector: 'chousen-guide',
    templateUrl: 'guide.html'
})

export class GuideComponent implements OnInit {
    constructor() { }
    showGuide = false;

    ngOnInit() { }

    toggleGuide() {
        this.showGuide = !this.showGuide;
    }
}
