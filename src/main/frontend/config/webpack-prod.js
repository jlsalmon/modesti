var loaders = require('./webpack-loaders');
var webpack = require('webpack');

module.exports = {
    entry: './src/main/frontend/app/app.ts',
    output: {
        path: '../../../build/resources/main/static/bin',
        filename: 'app.js'
    },
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
        }),
        new webpack.optimize.UglifyJsPlugin({
            compress: { warnings: true }
        })
    ]
};
