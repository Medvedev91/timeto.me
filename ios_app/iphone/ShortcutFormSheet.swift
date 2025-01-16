import SwiftUI
import shared

struct ShortcutFormSheet: View {
    
    @State private var vm: ShortcutFormSheetVm
    @Binding private var isPresented: Bool
    @State private var sheetHeaderScroll = 0
    
    init(
        isPresented: Binding<Bool>,
        editedShortcut: ShortcutDb?
    ) {
        _isPresented = isPresented
        vm = ShortcutFormSheetVm(shortcut: editedShortcut)
    }
    
    var body: some View {
        
        VMView(vm: vm, stack: .VStack(spacing: 0)) { state in
            
            SheetHeaderView(
                onCancel: { isPresented.toggle() },
                title: state.headerTitle,
                doneText: state.headerDoneText,
                isDoneEnabled: state.isHeaderDoneEnabled,
                scrollToHeader: sheetHeaderScroll
            ) {
                vm.save {
                    isPresented = false
                }
            }
            
            ScrollViewWithVListener(showsIndicators: false, vScroll: $sheetHeaderScroll) {
                
                VStack(spacing: 0) {
                    
                    VStack(spacing: 0) {
                        
                        MyListView__Padding__SectionSection()
                        
                        MyListView__HeaderView(title: state.inputNameHeader)
                        
                        MyListView__Padding__HeaderSection()
                        
                        MyListView__ItemView(
                            isFirst: true,
                            isLast: true
                        ) {
                            
                            MyListView__ItemView__TextInputView(
                                text: state.inputNameValue,
                                placeholder: state.inputNamePlaceholder,
                                isAutofocus: false,
                                onValueChanged: { newValue in
                                    vm.setInputNameValue(text: newValue)
                                }
                            )
                        }
                    }
                    
                    MyListView__HeaderView(title: state.inputUriHeader)
                        .padding(.top, 30)
                    
                    MyListView__Padding__HeaderSection()
                    
                    MyListView__ItemView(
                        isFirst: true,
                        isLast: true
                    ) {
                        
                        MyListView__ItemView__TextInputView(
                            text: state.inputUriValue,
                            placeholder: state.inputUriPlaceholder,
                            isAutofocus: false,
                            onValueChanged: { newValue in
                                vm.setInputUriValue(text: newValue)
                            }
                        )
                    }
                    
                    MyListView__HeaderView(title: "EXAMPLES")
                        .padding(.top, 60)
                    
                    MyListView__Padding__HeaderSection()
                    
                    ForEach(examples) { example in
                        let isFirst = examples.first!.id == example.id
                        MyListView__ItemView(
                            isFirst: isFirst,
                            isLast: examples.last!.id == example.id,
                            withTopDivider: !isFirst
                        ) {
                            MyListView__ItemView__ButtonView(
                                text: example.name,
                                rightView: AnyView(
                                    HStack {
                                        
                                        Text(example.hint)
                                            .foregroundColor(.secondary)
                                        
                                        if (state.inputUriValue == example.uri) {
                                            Image(systemName: "checkmark")
                                                .font(.system(size: 16, weight: .medium))
                                                .foregroundColor(.green)
                                        }
                                    }
                                        .padding(.trailing, 14)
                                )
                            ) {
                                vm.setInputNameValue(text: example.name)
                                vm.setInputUriValue(text: example.uri)
                                hideKeyboard()
                            }
                        }
                    }
                    
                    Spacer()
                        .frame(minHeight: 20)
                }
            }
        }
        .background(c.sheetBg)
    }
}

private let examples: [ShortcutExample] = [
    ShortcutExample(name: "10-Minute Meditation", hint: "Youtube", uri: "https://www.youtube.com/watch?v=O-6f5wQXSu8"),
    ShortcutExample(name: "Podcasts", hint: "Open App", uri: "https://podcasts.apple.com"),
    ShortcutExample(name: "Play a Song 😈", hint: "Music App", uri: "https://music.apple.com/ru/album/highway-to-hell/574043989?i=574044008&l=en"),
]

private struct ShortcutExample: Identifiable {
    
    let id: String
    
    let name: String
    let hint: String
    let uri: String
    
    init(
        name: String,
        hint: String,
        uri: String
    ) {
        id = "\(name)-\(hint)-\(uri)"
        self.name = name
        self.hint = hint
        self.uri = uri
    }
}
