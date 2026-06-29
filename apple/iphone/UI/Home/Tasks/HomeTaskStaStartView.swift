import SwiftUI
import shared

struct HomeTaskStaStartView: View {
    
    let homeTaskUi: HomeTasksItemUi.HomeTaskUi
    let onCancel: () -> Void
    
    ///
    
    @Environment(Navigation.self) private var navigation
    
    var body: some View {
        
        HStack {
            
            ZStack {
                Image(systemName: "xmark")
                    .font(.system(size: 15, weight: .medium))
                    .foregroundStyle(.blue)
            }
            .frame(
                width: HomeScreen__itemCircleHeight,
                height: HomeScreen__itemCircleHeight,
            )
            .background(Circle().fill(.white))
            .onTapGesture {
                onCancel()
            }
            .padding(.leading, 8)

            HStack {
                Text("Edit..")
                    .padding(.leading, 8)
                    .foregroundColor(.white)
                    .lineLimit(1)
                Spacer()
            }
            .onTapGesture {
                navigation.showTaskForm(strategy: homeTaskUi.editStrategy)
                onCancel()
            }
            .fillMaxHeight()
            .padding(.trailing, 8)
            
            ForEach(homeTaskUi.staTaskFoldersUi, id: \.self) { staFolderUi in
                HomeTasksFolderButton(
                    taskFolderUi: staFolderUi.taskFolderUi,
                    color: staFolderUi.isSelected ? .white : .secondary,
                    onClick: {
                        staFolderUi.onTap()
                        onCancel()
                    },
                )
            }

            HomeTasksCalendarButton(
                color: .secondary,
                onClick: {
                    navigation.fullScreen {
                        EventFormFullScreen(
                            initEventDb: nil,
                            initText: homeTaskUi.taskUi.taskDb.text,
                            initTime: nil,
                            onDone: {
                                homeTaskUi.taskUi.delete()
                            },
                        )
                    }
                    onCancel()
                },
            )
        }
        .fillMaxSize()
        .background(.blue)
    }
}
