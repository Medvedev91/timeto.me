import SwiftUI
import shared

private let checklistItemMinHeight: CGFloat = HomeScreen__itemHeight
private let itemFontSize: CGFloat = HomeScreen__primaryFontSize

struct ChecklistView: View {
    
    let checklistDb: ChecklistDb
    let maxLines: Int
    let withAddButton: Bool
    let onDelete: () -> Void
    
    ///
    
    @State private var uuid = UUID()
    
    var body: some View {
        VmView({
            ChecklistVm(
                checklistDb: checklistDb
            )
        }) { vm, state in
            ChecklistViewInner(
                vm: vm,
                state: state,
                maxLines: maxLines,
                withAddButton: withAddButton,
                onDelete: onDelete
            )
        }
        .id("vm_view_id_\(checklistDb.id)_\(uuid.uuidString)")
    }
}

private struct ChecklistViewInner: View {
    
    let vm: ChecklistVm
    let state: ChecklistVm.State
    
    let maxLines: Int
    let withAddButton: Bool
    let onDelete: () -> Void
    
    ///
    
    @Environment(Navigation.self) private var navigation
    
    private var stateIconType: ChecklistIconType {
        let stateUi = state.stateUi
        if stateUi is ChecklistStateUi.Completed {
            return .checked
        }
        if stateUi is ChecklistStateUi.Empty {
            return .unchecked
        }
        if stateUi is ChecklistStateUi.Partial {
            return .partial
        }
        fatalError()
    }
    
    var body: some View {
        
        HStack(alignment: .top) {

            List {
                
                ForEach(state.itemsUi, id: \.itemDb.id) { itemUi in
                    
                    Button(
                        action: {
                            itemUi.toggle()
                            Haptic.softShot()
                        },
                        label: {
                            HStack {
                                
                                ChecklistIconView(
                                    iconType: itemUi.itemDb.isChecked ? .checked : .unchecked
                                )
                                
                                Text(itemUi.text)
                                    .padding(.vertical, 4)
                                    .foregroundColor(.white)
                                    .font(.system(size: itemFontSize))
                                    .lineLimit(maxLines)
                                    .multilineTextAlignment(.leading)
                                
                                TriggersIconsView(
                                    checklistsDb: itemUi.textFeatures.checklistsDb,
                                    shortcutsDb: itemUi.textFeatures.shortcutsDb,
                                )
                                .padding(.leading, 8)
                                
                                Spacer()
                            }
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .frame(minHeight: checklistItemMinHeight)
                            .contentShape(Rectangle()) // Tap area full width
                        }
                    )
                    // Inner padding to contextMenu() correct clipping
                    .padding(.leading, HomeScreen__hPadding)
                    .customListItem()
                    .contextMenu {
                        
                        Section {
                            
                            Button(
                                action: {
                                    navigation.sheet {
                                        ChecklistFormItemSheet(
                                            checklistDb: state.checklistDb,
                                            checklistItemDb: itemUi.itemDb
                                        )
                                    }
                                },
                                label: {
                                    Label("Edit", systemImage: "square.and.pencil")
                                }
                            )
                            
                            Button(
                                role: .destructive,
                                action: {
                                    Haptic.warning()
                                    vm.deleteItem(
                                        itemDb: itemUi.itemDb,
                                        dialogsManager: navigation
                                    )
                                },
                                label: {
                                    Label("Delete", systemImage: "trash")
                                }
                            )
                        }
                        
                        Section(state.checklistDb.name) {
                            
                            Button(
                                action: {
                                    navigation.sheet {
                                        ChecklistFormItemSheet(
                                            checklistDb: state.checklistDb,
                                            checklistItemDb: nil
                                        )
                                    }
                                },
                                label: {
                                    Label("New Item", systemImage: "plus")
                                }
                            )
                            
                            Button(
                                action: {
                                    navigation.sheet {
                                        ChecklistFormItemsSheet(
                                            checklistDb: state.checklistDb,
                                            onDelete: {
                                                onDelete()
                                            }
                                        )
                                    }
                                },
                                label: {
                                    Label("Edit Checklist", systemImage: "gear")
                                }
                            )
                        }
                    }
                }
                .onMoveVm { oldIdx, newIdx in
                    vm.moveIos(fromIdx: oldIdx, toIdx: newIdx)
                }
                
                if withAddButton || state.itemsUi.isEmpty {
                    Button("New Item") {
                        navigation.sheet {
                            ChecklistFormItemSheet(
                                checklistDb: state.checklistDb,
                                checklistItemDb: nil
                            )
                        }
                    }
                    .foregroundColor(.blue)
                    .font(.system(size: itemFontSize))
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .frame(height: checklistItemMinHeight)
                    .padding(.leading, HomeScreen__hPadding)
                    .textAlign(.leading)
                    .customListItem()
                }
            }
            .customList()
            .scrollIndicators(.hidden)
            
            Button(
                action: {
                    state.stateUi.onClick()
                    Haptic.softShot()
                },
                label: {
                    
                    VStack {
                        
                        ChecklistIconView(
                            iconType: stateIconType
                        )
                        .frame(height: checklistItemMinHeight)
                        
                        Spacer()
                    }
                }
            )
        }
    }
}

private enum ChecklistIconType {
    case checked
    case unchecked
    case partial
}

private struct ChecklistIconView: View {
    
    let iconType: ChecklistIconType
    
    ///
    
    private var isFilled: Bool {
        iconType == .checked || iconType == .partial
    }
    
    var body: some View {
        ZStack {
            if iconType == .checked {
                Image(systemName: "checkmark")
                    .foregroundColor(.black)
                    .font(.system(size: 13, weight: .semibold))
            }
            else if iconType == .partial {
                Image(systemName: "minus")
                    .foregroundColor(.black)
                    .font(.system(size: 13, weight: .semibold))
            }
        }
        .frame(width: HomeScreen__itemCircleHeight, height: HomeScreen__itemCircleHeight)
        .background(
            Circle()
                .fill(isFilled ? .white : .clear)
                .strokeBorder(isFilled ? .clear : homeFgColor, lineWidth: 2)
                .background(.clear)
        )
        .padding(.trailing, HomeScreen__itemCircleMarginTrailing)
    }
}
