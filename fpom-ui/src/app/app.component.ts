import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { PortfolioDashboardComponent } from './components/portfolio-dashboard.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, PortfolioDashboardComponent],
  template: `
    <main>
      <app-portfolio-dashboard></app-portfolio-dashboard>
    </main>
    <router-outlet />
  `,
  styles: [`
    main {
      min-height: 100vh;
      background-color: #f5f5f5;
    }
  `]
})
export class AppComponent {
  title = 'Financial Peace of Mind - Portfolio Dashboard';
}
