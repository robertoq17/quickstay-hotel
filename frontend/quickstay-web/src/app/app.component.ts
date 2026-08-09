import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { RoomSearchComponent } from './features/room-search/room-search.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RoomSearchComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'quickstay-web';
}
