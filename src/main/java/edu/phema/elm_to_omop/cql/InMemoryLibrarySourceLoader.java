package edu.phema.elm_to_omop.cql;

import org.cqframework.cql.cql2elm.LibrarySourceLoader;
import org.cqframework.cql.cql2elm.LibrarySourceProvider;
import org.hl7.elm.r1.VersionedIdentifier;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InMemoryLibrarySourceLoader implements LibrarySourceLoader {
  HashMap<SourceHashKey, String> sourceMap;

  /**
   * Private has key gives use more control over matching
   */
  private class SourceHashKey {
    private VersionedIdentifier versionedIdentifier;

    public SourceHashKey(VersionedIdentifier versionedIdentifier) {
      this.versionedIdentifier = versionedIdentifier;
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof SourceHashKey)) {
        return false;
      }

      SourceHashKey that = (SourceHashKey) obj;
      return this.versionedIdentifier.getId().equals(that.versionedIdentifier.getId()) && this.versionedIdentifier.getVersion().equals(that.versionedIdentifier.getVersion());
    }

    @Override
    public int hashCode() {
      String hash = this.versionedIdentifier.getId() + this.versionedIdentifier.getVersion();

      return hash.hashCode();
    }
  }

  public InMemoryLibrarySourceLoader() {
    sourceMap = new HashMap<>();
  }

  public VersionedIdentifier getVersionedIdentifir(String cql) {
    String libraryRegex = "library\\s*\"?([a-zA-Z0-9.-]+)\"?\\s*version\\s*'([a-zA-Z0-9.-]+)'";

    Pattern pattern = Pattern.compile(libraryRegex);
    Matcher matcher = pattern.matcher(cql);

    matcher.find();

    // There has to be at least an id
    String id = matcher.group(1);

    String version;

    try {
      version = matcher.group(2);
    } catch (Exception e) {
      version = "";
    }

    VersionedIdentifier versionedIdentifier = new VersionedIdentifier();

    versionedIdentifier.setId(id);
    versionedIdentifier.setVersion(version);

    return versionedIdentifier;
  }

  public void addCql(String cql) {
    VersionedIdentifier id = getVersionedIdentifir(cql);

    sourceMap.put(new SourceHashKey(id), cql);
  }

  @Override
  public void clearProviders() {

  }

  @Override
  public InputStream getLibrarySource(VersionedIdentifier libraryIdentifier) {
    try {
      String source = sourceMap.get(new SourceHashKey(libraryIdentifier));

      return new ByteArrayInputStream(source.getBytes());
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public void registerProvider(LibrarySourceProvider provider) {

  }
}
