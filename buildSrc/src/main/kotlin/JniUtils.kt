import dev.nokee.platform.jni.JniLibraryDependencies
import org.gradle.nativeplatform.MachineArchitecture
import dev.nokee.runtime.nativebase.OperatingSystemFamily
import dev.nokee.runtime.nativebase.TargetMachine
import dev.nokee.language.base.tasks.SourceCompile
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.ModuleDependencyCapabilitiesHandler
import org.gradle.api.provider.Provider
import org.gradle.nativeplatform.toolchain.Clang
import org.gradle.nativeplatform.toolchain.Gcc
import org.gradle.nativeplatform.toolchain.VisualCpp

typealias OSFamily = org.gradle.nativeplatform.OperatingSystemFamily

fun MinimalExternalModuleDependency.dependencyNotation() =
    "${module.group}:${module.name}:${versionConstraint.requiredVersion}"

fun JniLibraryDependencies.jvmLibImplementation(notation: Provider<MinimalExternalModuleDependency>) {
    jvmImplementation(notation.map { it.dependencyNotation() }.get())
}

fun JniLibraryDependencies.nativeLibImplementation(notation: Provider<MinimalExternalModuleDependency>) {
    nativeImplementation(notation.map { it.dependencyNotation() }.get())
}

fun JniLibraryDependencies.nativeLibImplementation(
    notation: Provider<MinimalExternalModuleDependency>,
    action: Action<in ModuleDependency>
) {
    nativeImplementation(notation.map { it.dependencyNotation() }.get(), action)
}

fun ModuleDependencyCapabilitiesHandler.requireLibCapability(notation: Provider<MinimalExternalModuleDependency>) {
    requireCapabilities(notation.get().dependencyNotation())
}

val TargetMachine.variantName: String
    get() = "$osFamily-$architectureString"

val TargetMachine.osFamily: String
    get() = when {
        operatingSystemFamily.isWindows -> OSFamily.WINDOWS
        operatingSystemFamily.isLinux -> OSFamily.LINUX
        operatingSystemFamily.isMacOS -> OSFamily.MACOS
        else -> throw GradleException("Unknown operating system family '${operatingSystemFamily}'.")
    }

val TargetMachine.architectureString: String
    get() = if (architecture.is32Bit) MachineArchitecture.X86 else MachineArchitecture.X86_64

val TargetMachine.targetsHost: Boolean
    get() {
        val osName = System.getProperty("os.name").toLowerCase().replace(" ", "")
        val osFamily = operatingSystemFamily
        return when {
            osFamily.isWindows && osName.contains(OSFamily.WINDOWS) -> true
            osFamily.isLinux && osName.contains(OSFamily.LINUX) -> true
            osFamily.isMacOS && osName.contains(OSFamily.MACOS) -> true
            else -> false
        }
    }

fun libraryFileNameFor(project: Project, osFamily: OperatingSystemFamily): String = when {
    osFamily.isWindows -> "${project.name}.dll"
    osFamily.isLinux -> "lib${project.name}.so"
    osFamily.isMacOS -> "lib${project.name}.dylib"
    else -> throw GradleException("Unknown operating system family '${osFamily}'.")
}

fun SourceCompile.optimizedBinary() {
    compilerArgs.addAll(toolChain.map {
        when (it) {
            is Gcc, is Clang -> listOf("-O2")
            is VisualCpp -> listOf("/O2")
            else -> emptyList()
        }
    })
}
