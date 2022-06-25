package io.micronaut.inject.writer;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.inject.ast.Element;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

@Internal
public class DirectoryClassWriterOutputVisitor extends AbstractClassWriterOutputVisitor {
   private final File targetDir;

   public DirectoryClassWriterOutputVisitor(File targetDir) {
      super(true);
      this.targetDir = targetDir;
   }

   @Override
   public OutputStream visitClass(String classname, @Nullable Element originatingElement) throws IOException {
      return this.visitClass(classname, originatingElement);
   }

   @Override
   public OutputStream visitClass(String classname, Element... originatingElements) throws IOException {
      File targetFile = new File(this.targetDir, this.getClassFileName(classname)).getCanonicalFile();
      this.makeParent(targetFile.toPath());
      return Files.newOutputStream(targetFile.toPath());
   }

   @Override
   public void visitServiceDescriptor(String type, String classname, Element originatingElement) {
      String path = "META-INF/micronaut/" + type + "/" + classname;

      try {
         Path filePath = this.targetDir.toPath().resolve(path);
         this.makeParent(filePath);
         Files.write(filePath, "".getBytes(StandardCharsets.UTF_8), new OpenOption[]{StandardOpenOption.WRITE, StandardOpenOption.CREATE});
      } catch (IOException var6) {
         throw new ClassGenerationException("Unable to generate Bean entry at path: " + path, var6);
      }
   }

   @Override
   public Optional<GeneratedFile> visitMetaInfFile(String path, Element... originatingElements) {
      return Optional.ofNullable(this.targetDir).map(root -> new FileBackedGeneratedFile(new File(root, "META-INF" + File.separator + path)));
   }

   @Override
   public Optional<GeneratedFile> visitGeneratedFile(String path) {
      File parentFile = this.targetDir.getParentFile();
      File generatedDir = new File(parentFile, "generated");
      File f = new File(generatedDir, path);
      return f.getParentFile().mkdirs() ? Optional.of(new FileBackedGeneratedFile(f)) : Optional.empty();
   }

   private void makeParent(Path filePath) throws IOException {
      Path parent = filePath.getParent();
      if (!Files.exists(parent, new LinkOption[0])) {
         Files.createDirectories(parent);
      }

   }

   private String getClassFileName(String className) {
      return className.replace('.', File.separatorChar) + ".class";
   }
}
