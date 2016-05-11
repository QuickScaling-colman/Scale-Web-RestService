import React from 'react';
import ReactDOM from 'react-dom';
import Index from "./views/index.jsx";
import getMuiTheme from 'material-ui/styles/getMuiTheme';
import MuiThemeProvider from 'material-ui/styles/MuiThemeProvider';

window.React = React;

import injectTapEventPlugin from 'react-tap-event-plugin';

injectTapEventPlugin();

ReactDOM.render(<MuiThemeProvider muiTheme={getMuiTheme()}>
                  <Index />
                </MuiThemeProvider>, document.getElementById('app'));
