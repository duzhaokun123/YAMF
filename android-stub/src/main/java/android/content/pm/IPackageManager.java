package android.content.pm;

public interface IPackageManager {
    int getPackageUid(String s, long l, int i);
    PackageInfo getPackageInfo(String s, long l, int i);
}
