import SwiftUI
import shared

struct ShortcutFormSheet: View {

    @State private var vm: ShortcutFormSheetVM
    @Binding private var isPresented: Bool
    @State private var sheetHeaderScroll = 0

    init(
            isPresented: Binding<Bool>,
            editedShortcut: ShortcutModel?
    ) {
        _isPresented = isPresented
        vm = ShortcutFormSheetVM(shortcut: editedShortcut)
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

                    MyListView__HeaderView(title: state.inputNameHeader)
                            .padding(.top, MyListView.PADDING_SECTION_SECTION)

                    MyListView__SectionView {

                        MyListView__SectionView__TextInputView(
                                text: state.inputNameValue,
                                placeholder: state.inputNamePlaceholder,
                                isAutofocus: false,
                                onValueChanged: { newValue in
                                    vm.setInputNameValue(text: newValue)
                                }
                        )
                    }
                            .padding(.top, MyListView.PADDING_HEADER_SECTION)

                    MyListView__HeaderView(title: state.inputUriHeader)
                            .padding(.top, 30)

                    MyListView__SectionView {

                        MyListView__SectionView__TextInputView(
                                text: state.inputUriValue,
                                placeholder: state.inputUriPlaceholder,
                                isAutofocus: false,
                                onValueChanged: { newValue in
                                    vm.setInputUriValue(text: newValue)
                                }
                        )
                    }
                            .padding(.top, MyListView.PADDING_HEADER_SECTION)

                    MyListView__HeaderView(title: "EXAMPLES")
                            .padding(.top, 60)

                    MyListView__SectionView {

                        ForEach(examples) { example in
                            MyListView__SectionView__ButtonView(
                                    text: example.name,
                                    withTopDivider: examples.first!.id != example.id,
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
                            .padding(.top, MyListView.PADDING_HEADER_SECTION)

                    Spacer()
                            .frame(minHeight: 20)
                }
            }
        }
                .background(Color(.mySheetFormBg))
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
