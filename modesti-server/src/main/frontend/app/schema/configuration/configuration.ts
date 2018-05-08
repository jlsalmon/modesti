export class Configuration {
    public createFromUi: boolean;
    public showFieldsOnClone: boolean;

    public deserialize(config: Configuration): Configuration {
        this.createFromUi = config.createFromUi;
        this.showFieldsOnClone = config.showFieldsOnClone;
        
        return this;
    }

}