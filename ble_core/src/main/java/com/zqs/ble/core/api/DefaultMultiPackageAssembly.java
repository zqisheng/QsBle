package com.zqs.ble.core.api;

/*
 *   @author zhangqisheng
 *   @date 2022-07-28 22:12
 *   @description 默认的包组装实现，默认只考虑没有分包的情况
 */
public class DefaultMultiPackageAssembly extends BaseMultiPackageAssembly {
   @Override
   public void onError(Exception e) {
      e.printStackTrace();
   }

   @Override
   public boolean isLastPkg(byte[] value) {
      return true;
   }

   @Override
   public int getPkgIndex(byte[] value) {
      return 0;
   }
}
