import 'angular-cache';

export class CacheService {
  public static $inject: string[] = ['CacheFactory'];
  public filtersCache: any;
  public requestColumnsCache: any;

  public constructor(private CacheFactory: any) {
    if (!CacheFactory.get('filtersCache')) {
      this.createCache('filtersCache');
    }

    if (!CacheFactory.get('requestColumnsCache')) {
      this.createCache('requestColumnsCache');
    }

    this.filtersCache = CacheFactory.get('filtersCache');
    this.requestColumnsCache = CacheFactory.get('requestColumnsCache');
  }


  private createCache(cacheName: string) {
    this.CacheFactory.createCache(cacheName, {
      deleteOnExpire: 'aggressive',
      recycleFreq: 60000
    });
  }
}
