import {PolymerElement, html} from '@polymer/polymer';
import '@polymer/paper-icon-button/paper-icon-button.js';
import '@polymer/iron-icons/iron-icons.js';

class PaperIcon extends PolymerElement {
    static get template() {
        return html`
      <paper-icon-button icon="favorite"></paper-icon-button>
    `;
    }
}
customElements.define('paper-icon-button', PaperIcon);