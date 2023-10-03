import SwiftUI
import shared

struct NoteSheet: View {

    @EnvironmentObject private var nativeSheet: NativeSheet

    @Binding private var isPresented: Bool
    @State private var vm: NoteSheetVM

    init(
            isPresented: Binding<Bool>,
            initNote: NoteModel
    ) {
        _isPresented = isPresented
        _vm = State(initialValue: NoteSheetVM(note: initNote))
    }

    var body: some View {

        VMView(vm: vm, stack: .VStack()) { state in

            ScrollView {

                VStack {

                    Padding(vertical: 16)

                    Text(state.note.text)

                    Padding(vertical: 16)
                }
                        .padding(.horizontal, H_PADDING)
            }

            Spacer()

            Sheet__BottomView {

                HStack {

                    Spacer()

                    Sheet__BottomView__SecondaryButton(text: "Edit") {
                        nativeSheet.show { isEditPresented in
                            NoteFormSheet(
                                    isPresented: isEditPresented,
                                    note: state.note,
                                    onDelete: { isPresented = false }
                            )
                        }
                    }

                    Sheet__BottomView__SecondaryButton(text :"Close") {
                        isPresented = false
                    }
                }
                        .padding(.top, 10)
                        .padding(.trailing, MyListView.PADDING_OUTER_HORIZONTAL - 8)
                        .padding(.bottom, 10)
            }
        }
    }
}
