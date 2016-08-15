var loaders = require('./webpack-loaders');
var webpack = require('webpack');

module.exports = {
    entry: './app/app.ts',
    output: {
        path: '../resources/static/bin',
        filename: 'app.js'
    },
    devtool: 'eval',
    resolve: {
        modulesDirectories: ['../node_modules'],
        alias: {
            'ui-router': 'angular-ui-router/release/angular-ui-router.js'
        }
    },
    module: {
        loaders: loaders
    },
    plugins: [
        new webpack.ProvidePlugin({
            _: 'lodash',
            moment: 'moment'
        })
    ]
};
