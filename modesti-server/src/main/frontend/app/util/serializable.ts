interface ISerializable<T> {
  deserialize(json: Object): T;
}
