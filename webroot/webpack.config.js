var webpack = require('webpack');

module.exports = {
  devtool: "eval",
  entry: './app/app.jsx',
  output: {
    path: './public/js/',
    filename: 'bundle.js'
  },
  module: {
    loaders: [{
      test: /\.jsx?$/,
      exclude: /(node_modules|bower_components|public)/,
      loader: 'babel-loader',
      query: {
        presets: ['react', 'es2015']
      }
    }]
  }
};
