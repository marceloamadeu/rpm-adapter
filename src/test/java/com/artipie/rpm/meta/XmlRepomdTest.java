package com.artipie.rpm.meta;

import com.artipie.rpm.Digest;
import com.artipie.rpm.FileChecksum;
import com.artipie.rpm.NamingPolicy;
import com.artipie.rpm.pkg.MetadataFile;
import com.artipie.rpm.pkg.PackageOutput;
import com.jcabi.aspects.Tv;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

public class XmlRepomdTest {

    @Test
    public void testeCreateRepomd(@TempDir final Path temp) throws Exception {
        final Path fake = temp.resolve("fake.xml");
        final String openHex = new FileChecksum(fake, Digest.SHA1).hex();
        final long size = Files.size(fake);
        final Path repomdPath = temp.resolve("repomd.xml");
        final long currentTime = System.currentTimeMillis();
        final MetadataFile meta = new MetadataFile(
            XmlPackage.OTHER, new PackageOutput.FileOutput.Fake(fake).start()
        );
        try (final XmlRepomd repomd = new XmlRepomd(repomdPath)) {
            repomd.begin(currentTime);
            repomd.beginData("other");

            final Path gzip = meta.save(
                new NamingPolicy.HashPrefixed(Digest.SHA256), Digest.SHA256,
                repomd, Files.createDirectory(temp.resolve("meta"))
            );

        }



    }
}
