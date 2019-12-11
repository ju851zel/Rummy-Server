import {html, PolymerElement} from '@polymer/polymer/polymer-element.js';

/**
 * `tile-element`
 *
 *
 * @customElement
 * @polymer
 */
class TileElement extends PolymerElement {
    static get template() {
        return html`
      <style>
        .tileR {
            background-color: #ff8080;
            border-radius: 7px;
            padding: 10px 5px;
            margin: 5px;
            width: 65px;
            align-content: center;
            text-align: center;
            color: white;
            
        }
      </style>
      <div class="mx-1 align-content-center text-center tileR">
        <h3 class="text-white">[[tid]]</h3>
        <button class="btn btn-link">
            Move
        </button>
      </div>
    `;
    }

    static get properties() {
        return {
            tid: {
                type: String,
            },
        };
    }
}

window.customElements.define('tile-element', TileElement);
