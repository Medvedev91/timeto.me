import SwiftUI
import shared

struct ShortcutsPickerSheet: View {
    
    @Binding private var isPresented: Bool
    private let onPick: ([ShortcutDb]) -> Void
    
    @State private var vm: ShortcutsPickerSheetVm
    @State private var sheetHeaderScroll = 0
    
    init(
        isPresented: Binding<Bool>,
        selectedShortcuts: [ShortcutDb],
        onPick: @escaping ([ShortcutDb]) -> Void
    ) {
        self.onPick = onPick
        _isPresented = isPresented
        _vm = State(initialValue: ShortcutsPickerSheetVm(selectedShortcuts: selectedShortcuts))
    }
    
    var body: some View {
        
        VMView(vm: vm, stack: .VStack(spacing: 0)) { state in
            
            SheetHeaderView(
                onCancel: { isPresented = false },
                title: state.headerTitle,
                doneText: state.doneTitle,
                isDoneEnabled: true,
                scrollToHeader: sheetHeaderScroll
            ) {
                onPick(vm.getSelectedShortcuts())
                isPresented = false
            }
            
            ScrollViewWithVListener(showsIndicators: false, vScroll: $sheetHeaderScroll) {
                
                VStack(spacing: 0) {
                    
                    let shortcutsUI = state.shortcutsUI
                    ForEach(shortcutsUI, id: \.shortcut.id) { shortcutUI in
                        
                        let isFirst = shortcutsUI.first == shortcutUI
                        
                        MyListView__ItemView(
                            isFirst: isFirst,
                            isLast: shortcutsUI.last == shortcutUI,
                            withTopDivider: !isFirst
                        ) {
                            
                            MyListView__ItemView__CheckboxView(
                                text: shortcutUI.text,
                                isChecked: shortcutUI.isSelected
                            ) {
                                vm.toggleShortcut(shortcutUI: shortcutUI)
                            }
                        }
                    }
                }
                .padding(.top, 20)
            }
        }
        .background(c.sheetBg)
    }
}
