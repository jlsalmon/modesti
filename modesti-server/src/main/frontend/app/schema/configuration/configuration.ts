export class Configuration {
    public disableCreateFromUi: boolean;

    public deserialize(config: Configuration): Configuration {
        this.disableCreateFromUi = config.disableCreateFromUi;

        return this;
    }

}