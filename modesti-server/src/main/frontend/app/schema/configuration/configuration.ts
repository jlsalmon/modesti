export class Configuration {
    public createFromUi: boolean;
    public cloneFromUi: boolean;
    public showFieldsOnClone: boolean;

    public deserialize(config: Configuration): Configuration {
        this.createFromUi = config.createFromUi;
        this.cloneFromUi = config.cloneFromUi;
        this.showFieldsOnClone = config.showFieldsOnClone;
        
        return this;
    }

}