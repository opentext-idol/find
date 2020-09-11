import 'jquery'; // for bootstrap
import 'bootstrap';
import 'bootstrap/dist/css/bootstrap.css';
import './less/app.less';

import $ from 'jquery';
import data from './data.json';
import Polls from './polls/view';
import TopicsInterest from './topics-interest/view';
import Stories from './stories/view';

$(document).ready(function () {
    const pollsView = Polls.render(data.polls);
    pollsView.setElement($('.polls-view').get(0));
    pollsView.render();

    const topicsInterestView = TopicsInterest.render(data.topicsInterest);
    topicsInterestView.setElement($('.interests-view').get(0));
    topicsInterestView.render();

    const storiesView = Stories.render(data.stories);
    storiesView.setElement($('.stories-view').get(0));
    storiesView.render();
});
