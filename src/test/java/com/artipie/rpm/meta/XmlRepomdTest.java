package com.artipie.rpm.meta;

import com.artipie.rpm.Digest;
import com.artipie.rpm.FileChecksum;
import com.artipie.rpm.NamingPolicy;
import com.artipie.rpm.pkg.MetadataFile;
import com.artipie.rpm.pkg.PackageOutput;
import com.jcabi.aspects.Tv;
import com.jcabi.matchers.XhtmlMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class XmlRepomdTest {

    //repomd.xml: Essentially an index that contains the location, checksums, and timestamp of the other XML metadata files listed below.
    //other.xml.gz: Contains the changelog entries found in the RPM SPEC file for each package in the repository.

    @Test
    public void testeCreateValidateRepomd(@TempDir final Path temp) throws Exception {
        final Path fake = temp.resolve("fake.xml");

        final MetadataFile meta = new MetadataFile(
            XmlPackage.OTHER, new PackageOutput.FileOutput.Fake(fake).start()
        );

        final long currentTime = System.currentTimeMillis();
        final XmlRepomd repomd = new XmlRepomd(temp.resolve("repomd.xml"));
        repomd.begin(currentTime);
        repomd.beginData("other").timestamp(currentTime);

        final String openHex = new FileChecksum(fake, Digest.SHA1).hex();
        final long size = Files.size(fake);
        final Path gzip = meta.save(
            new NamingPolicy.HashPrefixed(Digest.SHA1), Digest.SHA1,
            repomd, Files.createDirectory(temp.resolve("meta"))
        );
        repomd.close();
        final String hex = new FileChecksum(gzip, Digest.SHA1).hex();

        MatcherAssert.assertThat(
            new String(Files.readAllBytes(repomd.file()), StandardCharsets.UTF_8),
            // @checkstyle LineLengthCheck (10 lines)
            XhtmlMatchers.hasXPaths(
                    "/*[local-name()='repomd']/*[local-name()='revision']",
                    String.format("/*[local-name()='repomd']/*[local-name()='data' and @type='other']/*[local-name()='checksum' and @type='sha' and text()='%s']", hex),
                    String.format("/*[local-name()='repomd']/*[local-name()='data' and @type='other']/*[local-name()='open-checksum' and @type='sha' and text()='%s']", openHex),
                    String.format("/*[local-name()='repomd']/*[local-name()='data' and @type='other']/*[local-name()='location' and @href='repodata/%s']", String.format("%s-%s.xml.gz", hex, XmlPackage.OTHER.filename())),
                    String.format("/*[local-name()='repomd']/*[local-name()='data' and @type='other']/*[local-name()='size' and text()='%d']", Files.size(gzip)),
                    String.format("/*[local-name()='repomd']/*[local-name()='data' and @type='other']/*[local-name()='open-size' and text()='%d']", size)
            )
        );

    }
}
