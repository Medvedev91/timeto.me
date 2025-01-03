import shared

enum NavigationPath: Hashable {
    case readme(defaultItem: ReadmeVm.DefaultItem)
    case whatsNew
}
