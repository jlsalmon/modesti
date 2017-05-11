var loaders = require('./webpack-loaders');
var webpack = require('webpack');

module.exports = {
    entry: {
        './build/resources/main/static/bin/app.js': './src/main/frontend/app/app.ts'
    },
    output: {
        path: '',
        filename: '[name]'
    },
    resolve: {
        modulesDirectories: ['./node_modules'],
        extensions: ['', '.webpack.js', '.web.js', '.ts', '.tsx', '.js'],
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
            moment: 'moment',
            $: 'jquery',
            jQuery: 'jquery',
            latinize: 'latinize'
        }),
        new webpack.optimize.UglifyJsPlugin({
            compress: { warnings: false }
        })
    ]
};
