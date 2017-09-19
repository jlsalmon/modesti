var webpack = require('webpack');

module.exports = {
    entry: './src/main/frontend/app/app.ts',
    output: {
        path: './build/resources/main/static/bin',
        filename: 'app.js'
    },
    devtool: 'source-map',
    resolve: {
        modulesDirectories: ['../node_modules'],
        extensions: ['', '.webpack.js', '.web.js', '.ts', '.tsx', '.js'],
        alias: {
            'ui-router': 'angular-ui-router/release/angular-ui-router.js'
        }
    },
    module: {
        loaders: [
            {
                test: /\.ts(x?)$/,
                loader: 'ts-loader',
                exclude: [/node_modules/, /static/]
            },
            {
                test: /\.(eot|woff|woff2|ttf|svg|png|jpg)$/,
                loader: 'url-loader?limit=30000&name=[name]-[hash].[ext]'
            },
            {
                test: /\.ts$/,
                loader: 'string-replace',
                query: {
                    multiple: [
                        { search: '/api/', replace: process.argv[5], flags: 'g' }
                    ]
                },
                exclude: [/node_modules/, /static/]
            }
        ]
    },
    plugins: [
        new webpack.ProvidePlugin({
            _: 'lodash',
            moment: 'moment',
            $: 'jquery',
            jQuery: 'jquery',
            latinize: 'latinize'
        })
    ]
};