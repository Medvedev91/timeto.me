import SwiftUI
import shared

private let checkboxSize = 21.0
private let checklistItemMinHeight: CGFloat = HomeScreen__itemHeight
private let itemFontSize: CGFloat = HomeScreen__primaryFontSize

struct ChecklistView: View {
    
    let checklistDb: ChecklistDb
    let maxLines: Int
    let withAddButton: Bool
    let onDelete: () -> Void
    
    var body: some View {
        VmView({
            ChecklistVm(checklistDb: checklistDb)
        }) { vm, state in
            ChecklistViewInner(
                vm: vm,
                state: state,
                maxLines: maxLines,
                withAddButton: withAddButton,
                onDelete: onDelete
            )
        }
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
    
    private var stateIconResource: String {
        let stateUi = state.stateUi
        if stateUi is ChecklistStateUi.Completed {
            return "checkmark.square.fill"
        }
        if stateUi is ChecklistStateUi.Empty {
            return "square"
        }
        if stateUi is ChecklistStateUi.Partial {
            return "minus.square.fill"
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
                        },
                        label: {
                            
                            HStack {
                                
                                Image(systemName: itemUi.itemDb.isChecked ? "checkmark.square.fill" : "square")
                                    .foregroundColor(c.white)
                                    .font(.system(size: checkboxSize, weight: .regular))
                                    .padding(.trailing, 10)
                                
                                Text(itemUi.itemDb.text)
                                    .padding(.vertical, 4)
                                    .foregroundColor(.white)
                                    .font(.system(size: itemFontSize))
                                    .lineLimit(maxLines)
                                    .textAlign(.leading)
                            }
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .frame(minHeight: checklistItemMinHeight)
                            .contentShape(Rectangle()) // Tap area full width
                        }
                    )
                    .customListItem()
                    .contextMenu {
                        Button(
                            action: {
                                navigation.sheet {
                                    ChecklistItemFormSheet(
                                        checklistDb: state.checklistDb,
                                        checklistItemDb: itemUi.itemDb
                                    )
                                }
                            },
                            label: {
                                Label("Edit", systemImage: "square.and.pencil")
                            }
                        )
                    }
                }
                .onMoveVm { oldIdx, newIdx in
                    vm.moveIos(fromIdx: oldIdx, toIdx: newIdx)
                }
                
                if withAddButton {
                    Button("New Item") {
                        navigation.sheet {
                            ChecklistItemFormSheet(
                                checklistDb: state.checklistDb,
                                checklistItemDb: nil
                            )
                        }
                    }
                    .foregroundColor(.blue)
                    .font(.system(size: itemFontSize))
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .frame(height: checklistItemMinHeight)
                    .textAlign(.leading)
                    .customListItem()
                }
            }
            .customList()
            .scrollIndicators(.hidden)
            
            Button(
                action: {
                    state.stateUi.onClick()
                },
                label: {
                    
                    VStack {
                        
                        Image(systemName: stateIconResource)
                            .foregroundColor(Color.white)
                            .font(.system(size: checkboxSize, weight: .regular))
                            .frame(height: checklistItemMinHeight)
                        
                        Button(
                            action: {
                                navigation.sheet {
                                    ChecklistItemsFormSheet(
                                        checklistDb: state.checklistDb,
                                        onDelete: {
                                            onDelete()
                                        }
                                    )
                                }
                            },
                            label: {
                                Image(systemName: "pencil")
                                    .foregroundColor(Color.white)
                                    .font(.system(size: checkboxSize, weight: .regular))
                                    .frame(height: checklistItemMinHeight)
                            }
                        )
                        
                        Spacer()
                    }
                }
            )
        }
        .padding(.horizontal, H_PADDING - 2)
    }
}
