import SwiftUI

struct SearchBar: UIViewRepresentable {
    
    @Binding var text: String
    let placeholder: String
    let returnKeyType: UIReturnKeyType
    let onDone: () -> Void
    
    ///
    
    func makeUIView(
        context: UIViewRepresentableContext<SearchBar>
    ) -> UISearchBar {
        let searchBar = UISearchBar(frame: .zero)
        searchBar.delegate = context.coordinator
        searchBar.searchBarStyle = .minimal
        searchBar.placeholder = placeholder
        searchBar.autocapitalizationType = .none
        searchBar.returnKeyType = returnKeyType
        return searchBar
    }
    
    func updateUIView(
        _ uiView: UISearchBar,
        context: UIViewRepresentableContext<SearchBar>
    ) {
        uiView.text = text
    }
    
    func makeCoordinator() -> SearchBar.Coordinator {
        Coordinator(
            text: $text,
            onDone: onDone
        )
    }
    
    class Coordinator: NSObject, UISearchBarDelegate {
        
        @Binding private var text: String
        private let onDone: () -> Void

        init(
            text: Binding<String>,
            onDone: @escaping () -> Void
        ) {
            _text = text
            self.onDone = onDone
        }
        
        func searchBar(
            _ searchBar: UISearchBar,
            textDidChange searchText: String
        ) {
            text = searchText
        }
        
        func searchBarSearchButtonClicked(
            _ searchBar: UISearchBar
        ) {
            onDone()
        }
    }
}
