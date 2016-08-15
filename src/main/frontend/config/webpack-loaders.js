module.exports = [
    {
        test: /\.ts(x?)$/,
        loader: 'ts-loader',
        exclude: [/node_modules/, /static/]
    },
    {
        test: /\.(eot|woff|woff2|ttf|svg|png|jpg)$/,
        loader: 'url-loader?limit=30000&name=[name]-[hash].[ext]'
    }
];
