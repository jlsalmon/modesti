import 'angular-cache';

export class CacheService {
  public static $inject: string[] = ['CacheFactory'];
  public filtersCache: any;
  public constructor(private CacheFactory: any) {

    if (!CacheFactory.get('filtersCache')) {
      CacheFactory.createCache('filtersCache', {
        deleteOnExpire: 'aggressive',
        recycleFreq: 60000
      });
    }

    this.filtersCache = CacheFactory.get('filtersCache');
  }
}
