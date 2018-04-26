export class Configuration {
    public createFromUi: boolean;

    public deserialize(config: Configuration): Configuration {
        this.createFromUi = config.createFromUi;

        return this;
    }

}