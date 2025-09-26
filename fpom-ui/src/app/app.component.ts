import { Component } from '@angular/core';
import { PortfolioDashboardComponent } from './components/portfolio-dashboard/portfolio-dashboard.component';

@Component({
  selector: 'app-root',
  imports: [PortfolioDashboardComponent],
  template: `
    <main>
      <app-portfolio-dashboard />
    </main>
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
