import 'jquery'; // for bootstrap
import 'bootstrap';
import 'bootstrap/dist/css/bootstrap.css';
import './less/app.less';

import _ from 'underscore';
import data from './data.json';
import Polls from './polls/view';
import TopicsInterest from './topics-interest/view';
import Stories from './stories/view';

_.each([
    Polls.render(data.polls),
    TopicsInterest.render(data.topicsInterest),
    Stories.render(data.stories)
], function (view) {
    document.body.appendChild(view.el);
    view.render();
});
